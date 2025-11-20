<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_POST['message_id'])) {
    $message_id = $_POST['message_id'];

    // Only delete if message is within 5 minutes
    $sql = "DELETE FROM messages 
            WHERE id = ? 
              AND TIMESTAMPDIFF(MINUTE, timestamp, NOW()) <= 5";

    $stmt = $conn->prepare($sql);

    if ($stmt) {
        $stmt->bind_param("i", $message_id);
        $stmt->execute();

        if ($stmt->affected_rows > 0) {
            $response['status'] = "success";
            $response['message'] = "Message deleted successfully";
        } else {
            $response['status'] = "failure";
            $response['message'] = "Delete window expired or invalid message ID";
        }

        $stmt->close();
    } else {
        $response['status'] = "failure";
        $response['message'] = "Statement preparation failed";
    }

    $conn->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Missing required field: message_id";
}

echo json_encode($response);
?>