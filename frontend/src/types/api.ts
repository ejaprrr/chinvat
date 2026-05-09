export interface ApiError {
  message: string;
  timestamp?: string;
}

// Generic API error response
export interface ApiErrorResponse {
  message: string;
  timestamp: string;
}
