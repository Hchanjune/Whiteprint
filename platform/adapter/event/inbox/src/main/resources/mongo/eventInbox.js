// Collection: event_inbox
// Document structure reference

db.createCollection("event_inbox");

db.event_inbox.createIndex({ event_type: 1, status: 1 });

// Document example:
// {
//     _id:              Long,         // event_id
//     trace_id:         String,
//     causation_id:     String | null,
//     occurred_at:      Date,
//     issuer:           String,
//     producer:         String,
//     schema_version:   String,
//     partition_key:    Long,
//     event_scope:      String,       // EventScope enum
//     event_type:       String,
//     payload:          BinData,
//     payload_json:     String,
//     metadata_json:    String,
//     status:           String,       // RECEIVED | PROCESSING | COMPLETED | FAILED | DEAD
//     received_at:      Date,
//     processed_at:     Date | null,
//     attempt_count:    Int,
//     last_attempted_at: Date | null,
//     error_message:    String | null
// }
