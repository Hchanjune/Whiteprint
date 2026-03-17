rootProject.name = "CQRS"

// Application
include("application:core:messaging")
include("application:core:security:provider")
include("application:core:security:verifier")
include("application:auth")
include("application:user:command")
include("application:user:query")

// Infra
include("infra:persistence:jpa")
include("infra:cache:redis")
include("infra:messaging:kafka")
include("infra:observability:omk-servlet")

// Core
include("core:kernel")
include("core:domain")
include("core:cache")
include("core:messaging")


include("infra:spring:servlet")
include("infra:spring:reactive")













