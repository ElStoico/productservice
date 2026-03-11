package com.product.productservice.logging;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;

public class CloudWatchLogsAppender extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

    private CloudWatchLogsClient client;
    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;
    private String logGroup;
    private String logStreamPrefix;
    private String logStreamName;

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setLogGroup(String logGroup) {
        this.logGroup = logGroup;
    }

    public void setLogStreamPrefix(String logStreamPrefix) {
        this.logStreamPrefix = logStreamPrefix;
    }

    @Override
    public void start() {
        if (isBlank(region) || isBlank(accessKey) || isBlank(secretKey) || isBlank(logGroup)) {
            addError("CloudWatch appender requiere region, accessKey, secretKey y logGroup");
            return;
        }

        try {
            CloudWatchLogsClientBuilder builder = CloudWatchLogsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));

            if (!isBlank(endpoint)) {
                builder.endpointOverride(URI.create(endpoint));
            }

            client = builder.build();

            logStreamName = buildLogStreamName();
            ensureLogGroup();
            ensureLogStream();
            super.start();
        } catch (Exception ex) {
            addError("No se pudo iniciar CloudWatchLogsAppender", ex);
        }
    }

    @Override
    protected synchronized void append(ILoggingEvent eventObject) {
        if (!isStarted() || client == null) {
            return;
        }

        try {
            InputLogEvent inputLogEvent = InputLogEvent.builder()
                    .timestamp(eventObject.getTimeStamp())
                    .message(formatEvent(eventObject))
                    .build();

            client.putLogEvents(PutLogEventsRequest.builder()
                    .logGroupName(logGroup)
                    .logStreamName(logStreamName)
                    .logEvents(inputLogEvent)
                    .build());
        } catch (SdkException ex) {
            addError("No se pudo enviar log a CloudWatch", ex);
        }
    }

    @Override
    public void stop() {
        if (client != null) {
            client.close();
        }
        super.stop();
    }

    private void ensureLogGroup() {
        try {
            client.createLogGroup(CreateLogGroupRequest.builder().logGroupName(logGroup).build());
        } catch (ResourceAlreadyExistsException ignored) {
        }
    }

    private void ensureLogStream() {
        try {
            client.createLogStream(CreateLogStreamRequest.builder()
                    .logGroupName(logGroup)
                    .logStreamName(logStreamName)
                    .build());
        } catch (ResourceAlreadyExistsException ignored) {
        }
    }

    private String buildLogStreamName() {
        String prefix = isBlank(logStreamPrefix) ? "app" : logStreamPrefix;
        return prefix + "-" + resolveHostName() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName().replaceAll("[^a-zA-Z0-9-]", "-");
        } catch (UnknownHostException ex) {
            return "unknown-host";
        }
    }

    private String formatEvent(ILoggingEvent eventObject) {
        StringBuilder builder = new StringBuilder()
                .append(TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(eventObject.getTimeStamp())))
                .append(" [")
                .append(eventObject.getThreadName())
                .append("] ")
                .append(eventObject.getLevel())
                .append(' ')
                .append(eventObject.getLoggerName())
                .append(" - ")
                .append(eventObject.getFormattedMessage());

        IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
        if (throwableProxy != null) {
            builder.append(System.lineSeparator())
                    .append(ThrowableProxyUtil.asString(throwableProxy));
        }

        return builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}