package com.smartresponse.api; import jakarta.validation.constraints.*; public record FeedbackRequest(@Min(1) @Max(5) int rating,@Size(max=1000) String comments){}
