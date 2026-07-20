package com.samaanlink.identity.application;

public record ResetPasswordCommand(String resetToken, String newPassword) {
}
