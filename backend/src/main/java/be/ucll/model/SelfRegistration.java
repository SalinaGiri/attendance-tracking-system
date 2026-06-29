package be.ucll.model;

public record SelfRegistration(Long eventId, String rNumber, String rotatingCode, RegistrationType registrationType) {}