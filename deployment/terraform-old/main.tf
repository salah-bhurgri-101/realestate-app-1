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
}

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

resource "aws_security_group" "public" {
  name        = "${var.project_name}-public"
  description = "Public gateway and frontend ingress"
  vpc_id      = aws_vpc.this.id
  tags        = local.common_tags

  ingress {
    description = "HTTP"
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
  description = "Private service-to-service traffic inside the VPC"
  vpc_id      = aws_vpc.this.id
  tags        = local.common_tags

  ingress {
    description = "All application traffic inside the VPC"
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = [var.vpc_cidr]
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

resource "aws_instance" "databases" {
  for_each = {
    auth-db     = { image = "postgres:16" }
    user-db     = { image = "postgres:16" }
    audit-db    = { image = "postgres:16" }
    property-db = { image = "mysql:8.4" }
    redis       = { image = "redis:7" }
  }

  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.ssh_key_name
  subnet_id                   = aws_subnet.public[0].id
  associate_public_ip_address = true
  vpc_security_group_ids     = [aws_security_group.internal.id]
  user_data = templatefile("${path.module}/templates/database.sh.tftpl", {
    role       = each.key
    image      = each.value.image
    dockerhub_username = var.dockerhub_username
    dockerhub_token = var.dockerhub_token
    auth_password = var.db_passwords.auth
    user_password = var.db_passwords.user
    audit_password = var.db_passwords.audit
    property_password = var.db_passwords.property
    mysql_password = var.db_passwords.mysql
    redis_password = var.db_passwords.redis
  })
  tags = merge(local.common_tags, { Name = "${var.project_name}-${each.key}" })
}

resource "aws_instance" "kafka" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.ssh_key_name
  subnet_id                   = aws_subnet.public[0].id
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.internal.id]
  user_data                   = file("${path.module}/templates/kafka.sh")
  tags                        = merge(local.common_tags, { Name = "${var.project_name}-kafka" })
}

resource "aws_instance" "services" {
  for_each = local.backend_images

  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.ssh_key_name
  subnet_id                   = aws_subnet.public[0].id
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.internal.id]
  user_data = templatefile("${path.module}/templates/service.sh.tftpl", {
    service            = each.key
    image              = each.value
    container_port     = local.service_ports[each.key]
    host_port          = local.service_ports[each.key]
    dockerhub_username = var.dockerhub_username
    dockerhub_token    = var.dockerhub_token
    jwt_secret         = var.jwt_secret
    cors_origins       = var.cors_origins
    mail_username      = var.mail_username
    mail_password      = var.mail_password
    redis_ip            = aws_instance.databases["redis"].private_ip
    kafka_ip            = aws_instance.kafka.private_ip
    db_host            = each.key == "auth-service" ? aws_instance.databases["auth-db"].private_ip : each.key == "user-service" ? aws_instance.databases["user-db"].private_ip : each.key == "audit-service" ? aws_instance.databases["audit-db"].private_ip : aws_instance.databases["property-db"].private_ip
    db_port            = each.key == "property-service" ? 3306 : 5432
    db_name            = each.key == "auth-service" ? "authdb" : each.key == "user-service" ? "user_service_db" : each.key == "audit-service" ? "auditdb" : "propertydb"
    db_username        = each.key == "property-service" ? "user" : "postgres"
    db_password        = each.key == "auth-service" ? var.db_passwords.auth : each.key == "user-service" ? var.db_passwords.user : each.key == "audit-service" ? var.db_passwords.audit : var.db_passwords.property
    redis_password     = var.db_passwords.redis
  })
  tags = merge(local.common_tags, { Name = "${var.project_name}-${each.key}" })
}

resource "aws_instance" "gateway" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.ssh_key_name
  subnet_id                   = aws_subnet.public[0].id
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.public.id]
  user_data = templatefile("${path.module}/templates/gateway.sh.tftpl", {
    image              = "${var.dockerhub_username}/realestate-api-gateway:${var.image_tag}"
    dockerhub_username = var.dockerhub_username
    dockerhub_token    = var.dockerhub_token
    jwt_secret         = var.jwt_secret
    cors_origins       = var.cors_origins
    redis_ip           = aws_instance.databases["redis"].private_ip
    redis_password     = var.db_passwords.redis
    auth_ip            = aws_instance.services["auth-service"].private_ip
    user_ip            = aws_instance.services["user-service"].private_ip
    property_ip        = aws_instance.services["property-service"].private_ip
    audit_ip           = aws_instance.services["audit-service"].private_ip
  })
  tags = merge(local.common_tags, { Name = "${var.project_name}-api-gateway" })
}

resource "aws_instance" "frontend" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.ssh_key_name
  subnet_id                   = aws_subnet.public[0].id
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.public.id]
  user_data = templatefile("${path.module}/templates/frontend.sh.tftpl", {
    image              = "${var.dockerhub_username}/realestate-auth-frontend:${var.image_tag}"
    dockerhub_username = var.dockerhub_username
    dockerhub_token    = var.dockerhub_token
  })
  tags = merge(local.common_tags, { Name = "${var.project_name}-auth-frontend" })
}
