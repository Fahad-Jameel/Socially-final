<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_POST['user_id'], $_POST['fcm_token'])) {
    $user_id = intval($_POST['user_id']);
    $fcm_token = trim($_POST['fcm_token']);

    // Insert or Update
    $sql = "INSERT INTO user_tokens (user_id, fcm_token) VALUES (?, ?)
            ON DUPLICATE KEY UPDATE fcm_token = VALUES(fcm_token)";
    $stmt = $conn->prepare($sql);

    if ($stmt) {
        $stmt->bind_param("is", $user_id, $fcm_token);
        if ($stmt->execute()) {
            $response['status'] = "success";
            $response['message'] = "Token saved successfully";
        } else {
            $response['status'] = "failure";
            $response['message'] = "Execution failed";
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
