<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_POST['message_id'], $_POST['new_content'])) {
    $message_id = $_POST['message_id'];
    $new_content = $_POST['new_content'];

    $sql = "UPDATE messages 
            SET message_content = ?, edited = TRUE 
            WHERE id = ? AND TIMESTAMPDIFF(MINUTE, timestamp, NOW()) <= 5";

    $stmt = $conn->prepare($sql);

    if ($stmt) {
        $stmt->bind_param("si", $new_content, $message_id);
        $stmt->execute();

        if ($stmt->affected_rows > 0) {
            $response['status'] = "success";
            $response['message'] = "Message edited successfully";
        } else {
            $response['status'] = "failure";
            $response['message'] = "Edit window expired or invalid message ID";
        }

        $stmt->close();
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
