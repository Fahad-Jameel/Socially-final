<?php
// File: send_notification.php
// Place this in your Notification folder in your API

include "../connect.php";
include "../headers.php";
// Check if all required parameters are present
if (!isset($_POST['token']) || !isset($_POST['title']) || !isset($_POST['body'])) {
    echo json_encode(['status' => 'error', 'message' => 'Missing required parameters']);
    exit;
}

$token = $_POST['token'];
$title = $_POST['title'];
$body = $_POST['body'];
$user_id = isset($_POST['user_id']) ? $_POST['user_id'] : '';
$sender_id = isset($_POST['sender_id']) ? $_POST['sender_id'] : '';
$notification_type = isset($_POST['notification_type']) ? $_POST['notification_type'] : 'message';

// Firebase API key - obtain this from Firebase Console
$server_key = '183254395373';

// Prepare the message
$message = [
    'to' => $token,
    'notification' => [
        'title' => $title,
        'body' => $body,
        'sound' => 'default',
        'badge' => '1'
    ],
    'data' => [
        'title' => $title,
        'body' => $body,
        'user_id' => $user_id,
        'sender_id' => $sender_id,
        'notification_type' => $notification_type,
        'click_action' => 'FLUTTER_NOTIFICATION_CLICK'
    ]
];

// Send notification using cURL
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Authorization: key=' . $server_key,
    'Content-Type: application/json'
]);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($message));

// Execute post
$result = curl_exec($ch);

if ($result === FALSE) {
    echo json_encode(['status' => 'error', 'message' => curl_error($ch)]);
} else {
    // Log the notification in the database (optional)
    try {
        $stmt = $conn->prepare("INSERT INTO notifications (user_id, sender_id, title, body, notification_type, created_at) 
                               VALUES (?, ?, ?, ?, ?, NOW())");
        $stmt->bind_param("iisss", $user_id, $sender_id, $title, $body, $notification_type);
        $stmt->execute();
        
        echo json_encode(['status' => 'success', 'response' => json_decode($result)]);
    } catch (Exception $e) {
        echo json_encode(['status' => 'partial_success', 'response' => json_decode($result), 'db_error' => $e->getMessage()]);
    }
}

curl_close($ch);
?>