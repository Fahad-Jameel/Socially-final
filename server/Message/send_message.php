<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_POST['sender_id'], $_POST['receiver_id'], $_POST['message_type'], $_POST['message_content'])) {
    $sender_id = $_POST['sender_id'];
    $receiver_id = $_POST['receiver_id'];
    $message_type = $_POST['message_type'];
    $message_content = $_POST['message_content'];

    $sql = "INSERT INTO messages (sender_id, receiver_id, message_type, message_content) VALUES (?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);

    if ($stmt) {
        $stmt->bind_param("iiss", $sender_id, $receiver_id, $message_type, $message_content);
        $stmt->execute();
        $stmt->close();

        // Get receiver's token
        $token_stmt = $conn->prepare("SELECT fcm_token FROM user_tokens WHERE user_id = ?");
        $token_stmt->bind_param("i", $receiver_id);
        $token_stmt->execute();
        $token_result = $token_stmt->get_result();

        if ($token_result->num_rows > 0) {
            $token_row = $token_result->fetch_assoc();
            $fcm_token = $token_row['fcm_token'];

            if (!empty($fcm_token)) {
                sendNotification($fcm_token, "New Message", $message_content);
            }
        }
        $token_stmt->close();

        $response['status'] = "success";
        $response['message'] = "Message sent successfully";

    } else {
        $response['status'] = "failure";
        $response['message'] = "Statement preparation failed";
    }

    $conn->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Missing required fields";
}

echo json_encode($response);
?>
