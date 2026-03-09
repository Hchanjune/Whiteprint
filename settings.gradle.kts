rootProject.name = "CQRS"

include("core-kernel")
include("core-web")
include("core-jpa")
include("core-jwt")

include("core-web:servlet")
include("core-web:reactive")

include("service-auth")

include("service-user:command")
include("service-user:query")

