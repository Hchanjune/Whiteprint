package org.whiteprint.platform.core.kernel.serializer

import com.google.protobuf.util.JsonFormat

object ProtobufSerializer {
    val printer: JsonFormat.Printer = JsonFormat.printer()
        .alwaysPrintFieldsWithNoPresence()
        .preservingProtoFieldNames()
        .omittingInsignificantWhitespace()

    val parser: JsonFormat.Parser = JsonFormat.parser()
        .ignoringUnknownFields()
}