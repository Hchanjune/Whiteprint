// Collection: event_outbox
// Document structure reference

db.createCollection("event_outbox");

db.event_outbox.createIndex({ status: 1, occurred_at: 1 });

// Document example:
// {
//     _id:            Long,           // event_id
//     trace_id:       String,
//     causation_id:   String | null,
//     occurred_at:    Date,
//     issuer:         String,
//     producer:       String,
//     schema_version: String,
//     partition_key:  Long,
//     event_scope:    String,         // EventScope enum
//     event_type:     String,
//     payload:        BinData,
//     payload_json:   String,
//     metadata_json:  String,
//     status:         String,         // PENDING | PROCESSING | PUBLISHED | FAILED
//     attempt_count:  Int,
//     last_attempted_at: Date | null
// }
