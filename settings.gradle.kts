rootProject.name = "Whiteprint"

// Application Sample
include("sample:auth")

include("sample:user:command")
include("sample:user:query")

// BOM
include("platform:bom")

// Platform Adapter
include("platform:adapter:serializer")

include("platform:adapter:persistence:servlet")
include("platform:adapter:persistence:reactive")

include("platform:adapter:event:outbox")
include("platform:adapter:event:inbox")
include("platform:adapter:event:publisher")
include("platform:adapter:event:subscriber")

include("platform:adapter:cache:servlet")
include("platform:adapter:cache:reactive")

include("platform:adapter:lock:distributed")

include("platform:adapter:security:provider:servlet")
include("platform:adapter:security:provider:reactive")
include("platform:adapter:security:verifier:servlet")
include("platform:adapter:security:verifier:reactive")

include("platform:adapter:web:servlet")
include("platform:adapter:web:reactive")

// Platform Infra
include("platform:infra:persistence:jpa")

include("platform:infra:cache:redis")

include("platform:infra:messaging:kafka")

include("platform:infra:observability:servlet")
include("platform:infra:observability:reactive")

include("platform:infra:client:armeria")

include("platform:infra:serializer:jackson")
include("platform:infra:serializer:protobuf")

include("platform:infra:security:jwt")

include("platform:infra:kms:vault")

// Platform Core
include("platform:core:kernel")
include("platform:core:domain")
include("platform:core:cache")
include("platform:core:messaging")
include("platform:core:kms")
include("platform:core:security")
