aws_region          = "us-east-1"
ami_id              = "ami-xxxxxxxxxxxxx"
ssh_key_name        = "aurelia-xys"
admin_cidr          = "223.211.222.130/32"
dockerhub_username  = "dockerhub_username"
image_tag           = "399a7e76a9bcaf92c497aca28892624284a8dc83"
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
