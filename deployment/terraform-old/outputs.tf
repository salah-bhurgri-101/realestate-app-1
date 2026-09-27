output "vpc_id" {
  value = aws_vpc.this.id
}

output "public_subnet_ids" {
  value = aws_subnet.public[*].id
}

output "service_public_ips" {
  value = merge(
    { for name, instance in aws_instance.services : name => instance.public_ip },
    { api-gateway = aws_instance.gateway.public_ip, auth-frontend = aws_instance.frontend.public_ip }
  )
}

output "service_private_ips" {
  value = merge(
    { for name, instance in aws_instance.services : name => instance.private_ip },
    { api-gateway = aws_instance.gateway.private_ip, auth-frontend = aws_instance.frontend.private_ip }
  )
}

output "database_private_ips" {
  value = { for name, instance in aws_instance.databases : name => instance.private_ip }
}

output "kafka_private_ip" {
  value = aws_instance.kafka.private_ip
}
