variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "project_name" {
  type    = string
  default = "aurelia"
}

variable "vpc_cidr" {
  type    = string
  default = "10.40.0.0/16"
}

variable "public_subnet_cidrs" {
  type    = list(string)
  default = ["10.40.1.0/24", "10.40.2.0/24"]
}

variable "availability_zones" {
  type    = list(string)
  default = ["us-east-1a", "us-east-1b"]
}

variable "private_subnet_cidrs" {
  type        = list(string)
  default     = ["10.40.11.0/24", "10.40.12.0/24"]
  description = "Private subnet(s) for databases, kafka, and backend services (no public IPs; egress via NAT gateway)."
}

variable "ami_id" {
  type        = string
  description = "Amazon Linux 2023 AMI ID for the selected region (must be free-tier eligible, e.g. t2.micro/t3.micro compatible)."
}

variable "instance_type" {
  type        = string
  default     = "t3.micro"
  description = "Free tier eligible instance type. Some accounts (esp. ones opened under AWS's current Free Tier plan) only allow t3.micro/t4g.micro, not the older t2.micro -- confirm with 'aws ec2 describe-instance-types --filters Name=free-tier-eligible,Values=true' before applying."
}

variable "ssh_key_name" {
  type        = string
  description = "Existing EC2 key pair name."
}

variable "admin_cidr" {
  type        = string
  description = "CIDR allowed to SSH to EC2 hosts."
}

variable "dockerhub_username" {
  type = string
}

variable "dockerhub_token" {
  type      = string
  sensitive = true
}

variable "image_tag" {
  type        = string
  default     = "latest"
  description = "SHA tag pushed by CI alongside :latest, kept only for manual rollback (docker run ...:<sha>). Production instances always run :latest -- this variable is not used to build the running image reference."
}

variable "jwt_secret" {
  type      = string
  sensitive = true
}

variable "cors_origins" {
  type        = string
  description = "Comma-separated list of allowed origins for the gateway/services. Scope this to the real frontend origin(s) in production -- don't use \"*\" once JWT auth is in front of it."
}

variable "mail_username" {
  type = string
}

variable "mail_password" {
  type      = string
  sensitive = true
}

variable "db_passwords" {
  type = object({
    auth     = string
    user     = string
    audit    = string
    property = string
    mysql    = string
    redis    = string
  })
  sensitive = true
}
