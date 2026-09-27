data "aws_caller_identity" "current" {}

locals {
  common_tags = {
    Project   = var.project_name
    ManagedBy = "terraform"
  }

  backend_images = {
    auth-service     = "${var.dockerhub_username}/realestate-auth-service:${var.image_tag}"
    user-service     = "${var.dockerhub_username}/realestate-user-service:${var.image_tag}"
    property-service = "${var.dockerhub_username}/realestate-property-service:${var.image_tag}"
    audit-service    = "${var.dockerhub_username}/realestate-audit-service:${var.image_tag}"
  }

  service_ports = {
    auth-service     = 8081
    user-service     = 8083
    property-service = 8082
    audit-service    = 8084
  }

  db_images = {
    auth-db     = "postgres:16"
    user-db     = "postgres:16"
    audit-db    = "postgres:16"
    property-db = "mysql:8.4"
    redis       = "redis:7"
  }

  # common vars every render of database.sh.tftpl needs, regardless of which
  # role's case branch actually fires
  db_common_vars = {
    dockerhub_username = var.dockerhub_username
    dockerhub_token     = var.dockerhub_token
    auth_password       = var.db_passwords.auth
    user_password       = var.db_passwords.user
    audit_password      = var.db_passwords.audit
    property_password   = var.db_passwords.property
    mysql_password       = var.db_passwords.mysql
    redis_password       = var.db_passwords.redis
  }

  postgres_host_ports = {
    auth-db  = 5432
    user-db  = 5433
    audit-db = 5434
  }
}

# ---------------------------------------------------------------------------
# Networking: public subnet (gateway, frontend, NAT) + private subnet
# (databases, kafka, services -- no public IPs, egress only via NAT)
# ---------------------------------------------------------------------------

resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags                 = merge(local.common_tags, { Name = "${var.project_name}-vpc" })
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
  tags   = merge(local.common_tags, { Name = "${var.project_name}-igw" })
}

resource "aws_subnet" "public" {
  count                   = length(var.public_subnet_cidrs)
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true
  tags                    = merge(local.common_tags, { Name = "${var.project_name}-public-${count.index + 1}" })
}

resource "aws_subnet" "private" {
  count                   = length(var.private_subnet_cidrs)
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.private_subnet_cidrs[count.index]
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = false
  tags                    = merge(local.common_tags, { Name = "${var.project_name}-private-${count.index + 1}" })
}

resource "aws_eip" "nat" {
  domain = "vpc"
  tags   = merge(local.common_tags, { Name = "${var.project_name}-nat-eip" })
}

resource "aws_nat_gateway" "this" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public[0].id
  tags          = merge(local.common_tags, { Name = "${var.project_name}-nat" })
  depends_on    = [aws_internet_gateway.this]
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }
  tags = merge(local.common_tags, { Name = "${var.project_name}-public-rt" })
}

resource "aws_route_table_association" "public" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.this.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.this.id
  }
  tags = merge(local.common_tags, { Name = "${var.project_name}-private-rt" })
}

resource "aws_route_table_association" "private" {
  count          = length(aws_subnet.private)
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id
}

# ---------------------------------------------------------------------------
# Security groups
# ---------------------------------------------------------------------------

resource "aws_security_group" "public" {
  name        = "${var.project_name}-public"
  description = "Edge: frontend + api-gateway ingress"
  vpc_id      = aws_vpc.this.id
  tags        = local.common_tags

  ingress {
    description = "API gateway (shares edge box with frontend on 80)"
    protocol    = "tcp"
    from_port   = 8080
    to_port     = 8080
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP (frontend only)"
    protocol    = "tcp"
    from_port   = 80
    to_port     = 80
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "HTTPS"
    protocol    = "tcp"
    from_port   = 443
    to_port     = 443
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "SSH administration"
    protocol    = "tcp"
    from_port   = 22
    to_port     = 22
    cidr_blocks = [var.admin_cidr]
  }
  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "internal" {
  name        = "${var.project_name}-internal"
  description = "Private subnet: DB/cache/queue/service traffic, VPC-internal only"
  vpc_id      = aws_vpc.this.id
  tags        = local.common_tags

  ingress {
    description = "Postgres (auth-db:5432, user-db:5433, audit-db:5434 on db-host)"
    protocol    = "tcp"
    from_port   = 5432
    to_port     = 5434  
    cidr_blocks = [var.vpc_cidr]
  }
  ingress {
    description = "MySQL"
    protocol    = "tcp"
    from_port   = 3306
    to_port     = 3306
    cidr_blocks = [var.vpc_cidr]
  }
  ingress {
    description = "Redis"
    protocol    = "tcp"
    from_port   = 6379
    to_port     = 6379
    cidr_blocks = [var.vpc_cidr]
  }
  ingress {
    description = "Kafka (broker + controller)"
    protocol    = "tcp"
    from_port   = 9092
    to_port     = 9093
    cidr_blocks = [var.vpc_cidr]
  }
  ingress {
    description = "Backend service ports"
    protocol    = "tcp"
    from_port   = 8081
    to_port     = 8084
    cidr_blocks = [var.vpc_cidr]
  }
  # No inbound SSH here on purpose -- these instances have no public IP, so
  # SSH from admin_cidr can't reach them anyway. Use SSM Session Manager
  # (IAM role attached below) instead: `aws ssm start-session --target <id>`.
  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ---------------------------------------------------------------------------
# SSM access for the private (no-public-IP) instances, since direct SSH
# from admin_cidr can't reach them. Traffic reaches SSM endpoints via NAT.
# ---------------------------------------------------------------------------

resource "aws_iam_role" "ssm" {
  name = "${var.project_name}-ec2-ssm-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.ssm.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "ssm" {
  name = "${var.project_name}-ec2-ssm-profile"
  role = aws_iam_role.ssm.name
}

# ---------------------------------------------------------------------------
# Instances 1-3/7 -- one Postgres role per box (your database.sh.tftpl
# hardcodes host port 5432, so these three can't share a box unmodified)
# ---------------------------------------------------------------------------

resource "aws_instance" "db_host" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.ssh_key_name
  subnet_id                   = aws_subnet.private[0].id
  associate_public_ip_address = false
  vpc_security_group_ids      = [aws_security_group.internal.id]
  iam_instance_profile        = aws_iam_instance_profile.ssm.name

  user_data = join("\n\n", [
    for role in ["auth-db", "user-db", "audit-db"] : templatefile("${path.module}/templates/database.sh.tftpl", merge(local.db_common_vars, {
      role      = role
      image     = local.db_images[role]
      host_port = local.postgres_host_ports[role]
    }))
  ])
  tags = merge(local.common_tags, { Name = "${var.project_name}-db-host" })
}

# ---------------------------------------------------------------------------
# Instance 4/7 -- data-secondary: property-db (3306) + redis (6379) +
# kafka (9092/9093) on one box. Different ports, so your unmodified
# templates run fine concatenated into a single user_data.
# ---------------------------------------------------------------------------

resource "aws_instance" "data_secondary" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.ssh_key_name
  subnet_id                   = aws_subnet.private[0].id
  associate_public_ip_address = false
  vpc_security_group_ids      = [aws_security_group.internal.id]
  iam_instance_profile        = aws_iam_instance_profile.ssm.name
  user_data = join("\n\n", [
    templatefile("${path.module}/templates/database.sh.tftpl", merge(local.db_common_vars, {
      role      = "property-db"
      image     = local.db_images["property-db"]
      host_port = 3306
    })),
    templatefile("${path.module}/templates/database.sh.tftpl", merge(local.db_common_vars, {
      role      = "redis"
      image     = local.db_images["redis"]
      host_port = 6379
    })),
    file("${path.module}/templates/kafka.sh"),
  ])
  tags = merge(local.common_tags, { Name = "${var.project_name}-data-secondary" })
}

# ---------------------------------------------------------------------------
# Instance 5/7 -- services-host: all 4 backend services on one box.
# service.sh.tftpl is already parameterized by host_port/container_port,
# so this needs zero template edits.
# ---------------------------------------------------------------------------

resource "aws_instance" "services_host" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.ssh_key_name
  subnet_id                   = aws_subnet.private[0].id
  associate_public_ip_address = false
  vpc_security_group_ids      = [aws_security_group.internal.id]
  iam_instance_profile        = aws_iam_instance_profile.ssm.name

  user_data = join("\n\n", [
    for svc, image in local.backend_images : templatefile("${path.module}/templates/service.sh.tftpl", {
      service            = svc
      image              = image
      container_port     = local.service_ports[svc]
      host_port          = local.service_ports[svc]
      dockerhub_username = var.dockerhub_username
      dockerhub_token    = var.dockerhub_token
      jwt_secret         = var.jwt_secret
      cors_origins       = var.cors_origins
      mail_username      = var.mail_username
      mail_password      = var.mail_password
      redis_ip           = aws_instance.data_secondary.private_ip
      redis_password     = var.db_passwords.redis
      kafka_ip           = aws_instance.data_secondary.private_ip
      db_host = (
        svc == "property-service" ? aws_instance.data_secondary.private_ip :
        aws_instance.db_host.private_ip
      )
      db_port = (
        svc == "auth-service"  ? local.postgres_host_ports["auth-db"] :
        svc == "user-service"  ? local.postgres_host_ports["user-db"] :
        svc == "audit-service" ? local.postgres_host_ports["audit-db"] :
        3306
      )
      db_name     = svc == "auth-service" ? "authdb" : svc == "user-service" ? "user_service_db" : svc == "audit-service" ? "auditdb" : "propertydb"
      db_username = svc == "property-service" ? "user" : "postgres"
      db_password = (
        svc == "auth-service" ? var.db_passwords.auth :
        svc == "user-service" ? var.db_passwords.user :
        svc == "audit-service" ? var.db_passwords.audit :
        var.db_passwords.property
      )
    })
  ])
  tags = merge(local.common_tags, { Name = "${var.project_name}-services-host" })
}

# ---------------------------------------------------------------------------
# Instances 6-7/7 -- public edge: api-gateway + frontend, each their own box
# (both bind host port 80, so they can't be combined without changing one)
# ---------------------------------------------------------------------------

resource "aws_instance" "edge" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.ssh_key_name
  subnet_id                   = aws_subnet.public[0].id
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.public.id]
  iam_instance_profile        = aws_iam_instance_profile.ssm.name

  user_data = join("\n\n", [
    templatefile("${path.module}/templates/frontend.sh.tftpl", {
      image              = "${var.dockerhub_username}/realestate-auth-frontend:${var.image_tag}"
      dockerhub_username = var.dockerhub_username
      dockerhub_token    = var.dockerhub_token
    }),
    templatefile("${path.module}/templates/gateway.sh.tftpl", {
      image              = "${var.dockerhub_username}/realestate-api-gateway:${var.image_tag}"
      dockerhub_username = var.dockerhub_username
      dockerhub_token    = var.dockerhub_token
      jwt_secret         = var.jwt_secret
      cors_origins       = var.cors_origins
      redis_ip           = aws_instance.data_secondary.private_ip
      redis_password     = var.db_passwords.redis
      auth_ip            = aws_instance.services_host.private_ip
      user_ip            = aws_instance.services_host.private_ip
      property_ip        = aws_instance.services_host.private_ip
      audit_ip           = aws_instance.services_host.private_ip
    }),
  ])
  tags = merge(local.common_tags, { Name = "${var.project_name}-edge" })
}
