------------------------------------------------------------------------------
-- DELETES A MESSAGE FROM AN INBOX
------------------------------------------------------------------------------

DELETE
FROM inbox
WHERE user_id = ?
      AND message_id = ?