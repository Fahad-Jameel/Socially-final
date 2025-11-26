<?php
include "../connect.php";
include "../headers.php";

$response = array();

// Read POST input
$user_id = $_POST['user_id'] ?? null;
$channel_name = $_POST['channel_name'] ?? null;

// Validate input
if (!$user_id || !$channel_name) {
    $response['status'] = "failure";
    $response['message'] = "user_id and channel_name must be provided";
    echo json_encode($response);
    exit();
}

// Agora credentials
$app_id = "b1ea0506ab3b48e7b8858573be4e3ae6";
$app_certificate = "c04a903217234b44a8e31c76cd74d9e9";

// Token expiration time (24 hours from now)
$expiration_time = time() + 86400; // 24 hours

// Generate Agora RTC token
// Note: This uses Agora's token generation algorithm
// For production, consider using Agora's official PHP SDK: https://github.com/AgoraIO/Tools/tree/master/DynamicKey/AgoraDynamicKey

function base64UrlEncode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function generateToken($app_id, $app_certificate, $channel_name, $uid, $expiration_time) {
    // Agora token format: base64Url(header).base64Url(payload).base64Url(signature)
    
    $header = array(
        "alg" => "HS256",
        "typ" => "JWT"
    );
    
    $payload = array(
        "iss" => $app_id,
        "exp" => $expiration_time,
        "iat" => time(),
        "aud" => "rtc",
        "sub" => $app_id,
        "channel" => $channel_name,
        "uid" => (string)$uid
    );
    
    // Encode header and payload using base64Url
    $header_encoded = base64UrlEncode(json_encode($header));
    $payload_encoded = base64UrlEncode(json_encode($payload));
    
    // Create signature
    $signature_input = $header_encoded . "." . $payload_encoded;
    $signature = base64UrlEncode(hash_hmac('sha256', $signature_input, $app_certificate, true));
    
    // Combine to create token
    $token = $header_encoded . "." . $payload_encoded . "." . $signature;
    
    return $token;
}

try {
    $token = generateToken($app_id, $app_certificate, $channel_name, $user_id, $expiration_time);
    
    $response['status'] = "success";
    $response['token'] = $token;
    $response['expiration_time'] = $expiration_time;
    $response['message'] = "Token generated successfully";
} catch (Exception $e) {
    $response['status'] = "failure";
    $response['message'] = "Failed to generate token: " . $e->getMessage();
}

echo json_encode($response);
?>
