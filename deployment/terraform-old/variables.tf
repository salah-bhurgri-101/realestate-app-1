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

  variable "ami_id" {
    type        = string
    description = "Amazon Linux 2023 AMI ID for the selected region."
  }

  variable "instance_type" {
    type    = string
    default = "t3.micro"
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
    type    = string
    default = "latest"
  }

  variable "jwt_secret" {
    type      = string
    sensitive = true
  }

  variable "cors_origins" {
    type = string
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
