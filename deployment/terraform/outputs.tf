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
  description = "frontend on :80, gateway on :8080, same host -- now backed by a stable Elastic IP (aws_eip.edge), not the launch-time dynamic address"
  value = {
    edge = aws_eip.edge.public_ip
  }
}

output "private_ips" {
  value = {
    db-host        = aws_instance.db_host.private_ip        # auth-db:5432, user-db:5433, audit-db:5434
    data-secondary = aws_instance.data_secondary.private_ip # property-db:3306, redis:6379, kafka:9092-93
    services-host  = aws_instance.services_host.private_ip
    edge           = aws_instance.edge.private_ip # frontend:80, gateway:8080
  }
}

# Consumed by the CI/CD "deploy" job: SSH targets the edge host's stable
# public IP, and SSM send-command targets the private hosts by instance ID
# (they have no public IP / SSH ingress, by design). Store these as GitHub
# secrets (EC2_EDGE_HOST, EC2_SERVICES_HOST_ID) after every apply that
# replaces an instance.
output "instance_ids" {
  description = "Instance IDs for aws ssm send-command targets (private hosts) and reference."
  value = {
    edge           = aws_instance.edge.id
    db_host        = aws_instance.db_host.id
    data_secondary = aws_instance.data_secondary.id
    services_host  = aws_instance.services_host.id
  }
}

output "ssm_connect_hint" {
  description = "Private instances have no public IP or SSH ingress -- connect via SSM instead"
  value       = "aws ssm start-session --target <instance-id>"
}
