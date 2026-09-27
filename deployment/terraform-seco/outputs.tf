output "vpc_id" {
  value = aws_vpc.this.id
}

output "public_subnet_ids" {
  value = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  value = aws_subnet.private[*].id
}

output "nat_gateway_public_ip" {
  value = aws_eip.nat.public_ip
}

output "instance_count" {
  description = "4 instances x 2 vCPUs (t3.micro) = 8 vCPUs, at the account limit"
  value       = 4
}

output "public_ips" {
  description = "frontend on :80, gateway on :8080, same host"
  value = {
    edge = aws_instance.edge.public_ip
  }
}

output "private_ips" {
  value = {
    db-host        = aws_instance.db_host.private_ip        # auth-db:5432, user-db:5433, audit-db:5434
    data-secondary = aws_instance.data_secondary.private_ip # property-db:3306, redis:6379, kafka:9092-93
    services-host  = aws_instance.services_host.private_ip
    edge           = aws_instance.edge.private_ip           # frontend:80, gateway:8080
  }
}

output "ssm_connect_hint" {
  description = "Private instances have no public IP or SSH ingress -- connect via SSM instead"
  value       = "aws ssm start-session --target <instance-id>"
}