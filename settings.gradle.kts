rootProject.name = "Whiteprint"

// Application
include("service:auth")
include("service:user:command")
include("service:user:query")

// Platform Adapter
include("platform:adapter:messaging:outbox")
include("platform:adapter:messaging:producer")
include("platform:adapter:lock:distributed")
include("platform:adapter:security:provider:core")
include("platform:adapter:security:provider:servlet")
include("platform:adapter:security:provider:reactive")
include("platform:adapter:security:verifier:core")
include("platform:adapter:security:verifier:servlet")
include("platform:adapter:security:verifier:reactive")
include("platform:adapter:web:servlet")
include("platform:adapter:web:reactive")

// Platform Infra
include("platform:infra:persistence:jpa")
include("platform:infra:cache:redis")
include("platform:infra:messaging:kafka")
include("platform:infra:observability:servlet")

// Platform Core
include("platform:core:kernel")
include("platform:core:domain")
include("platform:core:cache")
include("platform:core:messaging")