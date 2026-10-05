package com.paymentology.live_coding;

import java.util.List;

public record CardActivityPage(List<CardActivityItem> items, int page, int size, long totalElements) {
}
