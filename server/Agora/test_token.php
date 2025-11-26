<?php
// Test script to verify Agora credentials and token generation

$app_id = "b1ea0506ab3b48e7b8858573be4e3ae6";
$app_certificate = "c04a903217234b44a8e31c76cd74d9e9";
$channel_name = "testchannel";
$uid = 123;

echo "Testing Agora Token Generation\n";
echo "==============================\n\n";
echo "App ID: $app_id\n";
echo "App Certificate: $app_certificate\n";
echo "Channel: $channel_name\n";
echo "UID: $uid\n\n";

// Token expiration time (24 hours from now)
$expiration_time = time() + 86400;

function base64UrlEncode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function generateToken($app_id, $app_certificate, $channel_name, $uid, $expiration_time) {
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
    $token = generateToken($app_id, $app_certificate, $channel_name, $uid, $expiration_time);
    
    echo "Token Generated Successfully!\n";
    echo "Token: $token\n\n";
    
    // Decode to verify
    $parts = explode('.', $token);
    if (count($parts) == 3) {
        $header_decoded = json_decode(base64_decode(strtr($parts[0], '-_', '+/')), true);
        $payload_decoded = json_decode(base64_decode(strtr($parts[1], '-_', '+/')), true);
        
        echo "Header: " . json_encode($header_decoded, JSON_PRETTY_PRINT) . "\n\n";
        echo "Payload: " . json_encode($payload_decoded, JSON_PRETTY_PRINT) . "\n\n";
        
        // Verify signature
        $signature_input = $parts[0] . "." . $parts[1];
        $expected_signature = base64UrlEncode(hash_hmac('sha256', $signature_input, $app_certificate, true));
        
        if ($parts[2] === $expected_signature) {
            echo "✓ Signature verified successfully!\n";
        } else {
            echo "✗ Signature verification failed!\n";
            echo "Expected: $expected_signature\n";
            echo "Got: " . $parts[2] . "\n";
        }
    }
    
    echo "\nToken length: " . strlen($token) . " characters\n";
    echo "Token expires at: " . date('Y-m-d H:i:s', $expiration_time) . "\n";
    
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
?>

