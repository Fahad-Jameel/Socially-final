<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_GET['chat_id'])) {
    $chat_id = $_GET['chat_id'];

    $sql = "SELECT id, sender_id, receiver_id, media, timestamp, message_type 
            FROM messages 
            WHERE chat_id = ? AND message_type = 'image' 
            ORDER BY timestamp ASC";

    $stmt = $conn->prepare($sql);

    if ($stmt) {
        $stmt->bind_param("i", $chat_id);
        $stmt->execute();
        $result = $stmt->get_result();

        $mediaMessages = array();
        while ($row = $result->fetch_assoc()) {
            $mediaMessages[] = $row;
        }

        $response['status'] = "success";
        $response['media_messages'] = $mediaMessages;
    } else {
        $response['status'] = "failure";
        $response['message'] = "Statement preparation failed";
    }

    $stmt->close();
    $conn->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Missing required field: chat_id";
}

echo json_encode($response);
?>