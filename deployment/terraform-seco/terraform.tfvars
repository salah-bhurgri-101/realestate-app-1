aws_region          = "us-east-1"
ami_id              = "ami-xxxxxxxxxxxxxxxxx"
instance_type      = "t3.micro" # confirm with: aws ec2 describe-instance-types --filters Name=free-tier-eligible,Values=true 
ssh_key_name        = "aurelia-asas"
admin_cidr          = "203.0.113.10/32"
dockerhub_username  = "your-dockerhub-user"
image_tag           = "b148530b33c00141e891102682ad698aae413c"
jwt_secret          = "replace-with-a-random-secret-at-least-32-bytes"
cors_origins        = "*"
mail_username       = "smtp-user"
mail_password       = "smtp-app-password"

db_passwords = {
  auth     = "replace-auth-password"
  user     = "replace-user-password"
  audit    = "replace-audit-password"
  property = "replace-property-password"
  mysql    = "replace-mysql-root-password"
  redis    = "replace-redis-password"
}
