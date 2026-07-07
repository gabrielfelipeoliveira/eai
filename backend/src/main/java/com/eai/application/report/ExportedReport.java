package com.eai.application.report;

public record ExportedReport(String filename, String contentType, byte[] content) {
}
