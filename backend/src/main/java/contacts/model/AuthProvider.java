package contacts.model;

public enum AuthProvider {
    GOOGLE,
    // No longer issued — kept so existing user documents with this value still deserialize.
    EMAIL_PASSWORD
}
