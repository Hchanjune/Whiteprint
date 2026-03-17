rootProject.name = "Whiteprint"

// Application
include("application:auth")
include("application:user:command")
include("application:user:query")

// Platform Adapter
include("platform:adapter:messaging")
include("platform:adapter:security:provider")
include("platform:adapter:security:verifier")
include("platform:adapter:web:servlet")
include("platform:adapter:web:reactive")

// Platform Infra
include("platform:infra:persistence:jpa")
include("platform:infra:cache:redis")
include("platform:infra:messaging:kafka")
include("platform:infra:observability:omk-servlet")

// Platform Core
include("platform:core:kernel")
include("platform:core:domain")
include("platform:core:cache")
include("platform:core:messaging")













