# Protected profile storage

Personal names, realm names, demographic selections, contact/address fields, employment,
medical details and additional profile attributes are written through involved-party
identification-type links. Each field has a dedicated `ProfileAttribute...` or
`ProfileName...` identification type. Choice definitions remain public classifications
in separate profile-owned data concepts; a party's selection is an encrypted value.
Biography is non-sensitive and remains an ordinary FSDM classification value.

Populated profile writes require `activitymaster.encryption.mode=aes-gcm` or `enterprise`
and the corresponding ActivityMaster key configuration. Provision keys externally;
never place production keys in source control. The legacy encoding mode cannot save
populated profiles. Clearing a field archives its active link without adding a value.

The stateless profile installer adds the identification types without changing shared
FSDM column sizes. Identification ciphertext must fit the existing 200-character column;
overlength sensitive values are rejected before persistence.

Protected links receive restricted administrative/system/application/plugin grants.
NE1's authenticated profile boundary still verifies the actor and authorizes access;
encryption does not replace authorization.

Legacy values remain readable. Supplying a legacy field to a save retires its old link
and writes the protected replacement. Untouched legacy records and archived plaintext
history are not automatically rewritten or purged. A separate migration and retention
decision is needed for existing production data; these changes alone do not establish
GDPR compliance.
