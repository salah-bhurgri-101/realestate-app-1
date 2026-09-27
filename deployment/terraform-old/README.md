# Aurelia EC2-only AWS deployment

This Terraform stack uses AWS only for the VPC networking and EC2 compute
needed to run the platform. It does **not** use ECS, EKS, RDS, ElastiCache,
MSK, Lambda, S3, or any other managed application/data service.

## EC2 layout

One EC2 host is created for each application service:

- `api-gateway`
- `auth-service`
- `user-service`
- `property-service`
- `audit-service`
- `auth-frontend`

Separate EC2 hosts run `auth-db`, `user-db`, `audit-db`, `property-db`, Redis,
and Kafka as Docker containers.

All hosts are placed in VPC public subnets so they can bootstrap Docker
without NAT Gateway. Security groups expose HTTP/HTTPS only on gateway and
frontend hosts; application and database traffic is restricted to the VPC
CIDR. SSH is restricted to `admin_cidr`.

## Apply

```bash
cp terraform.tfvars.example terraform.tfvars
# Replace every value, especially ami_id, passwords, JWT, SMTP, and admin_cidr.
terraform init
terraform fmt -recursive
terraform validate
terraform plan -out aurelia.tfplan
terraform apply aurelia.tfplan
terraform output
```

Never commit `terraform.tfvars`, plan files, or Terraform state. Use an
encrypted remote state backend when this is moved into a team environment.

## GitHub Actions secrets

The deploy job expects:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `EC2_SSH_PRIVATE_KEY`
- `EC2_GATEWAY_HOST`
- `EC2_FRONTEND_HOST`
- `EC2_AUTH_HOST`
- `EC2_USER_HOST`
- `EC2_PROPERTY_HOST`
- `EC2_AUDIT_HOST`

The workflow builds/tests all Java modules and the frontend, pushes immutable
`github.sha` tags plus `latest` to Docker Hub, then SSHes to application EC2
hosts to pull the immutable tag and restart the corresponding systemd unit.
Each systemd unit recreates its container, so a restart actually runs the
newly pulled image rather than the old container filesystem.

The Terraform apply itself is intentionally documented as an operator step.
Do not let every application push recreate VPCs and EC2 hosts; provision the
infrastructure first, store its state safely, then enable the protected
production deployment environment.

## Important hardening before production

- Add a dedicated Kafka EC2 host and configure its private IP in the service
  environment.
- Use private subnets with a self-managed NAT instance if public bootstrap
  addresses are unacceptable; do not open database ports to the internet.
- Put TLS/reverse-proxy configuration in front of gateway and frontend.
- Store Terraform state remotely and encrypt/lock it.
- Do not pass long-lived Docker Hub tokens or database passwords through
  Terraform state in a real production setup; use short-lived bootstrap
  credentials and rotate them.
