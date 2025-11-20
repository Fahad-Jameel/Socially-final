
<?php
include "../connect.php";
include "../headers.php";
if (!isset($_POST['user_id']) || !isset($_POST['fcm_token'])) {
    echo json_encode(['status' => 'error', 'message' => 'User ID and FCM token are required']);
    exit;
}

$user_id = $_POST['user_id'];
$fcm_token = $_POST['fcm_token'];

try {
    // First, check if the users table has an fcm_token column
    $result = $conn->query("SHOW COLUMNS FROM users LIKE 'fcm_token'");
    if ($result->num_rows == 0) {
        // Add the fcm_token column if it doesn't exist
        $conn->query("ALTER TABLE users ADD COLUMN fcm_token VARCHAR(255)");
    }
    
    // Update the token
    $stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");
    $stmt->bind_param("si", $fcm_token, $user_id);
    $stmt->execute();
    
    if ($stmt->affected_rows > 0) {
        echo json_encode(['status' => 'success', 'message' => 'FCM token updated successfully']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Failed to update FCM token']);
    }
} catch (Exception $e) {
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
}
?>