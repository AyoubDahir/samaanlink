package com.samaanlink.identity.application;

public record LoginCommand(String email, String rawPassword) {
}
