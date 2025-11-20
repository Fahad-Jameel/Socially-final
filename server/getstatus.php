<?php
include "./connect.php";
include "./headers.php";

$response = array();

// Accept either GET or POST for flexibility
$user_id = $_REQUEST['id'] ?? null;

if (!$user_id) {
    $response['status'] = "failure";
    $response['message'] = "User ID (id) not provided.";
    echo json_encode($response);
    exit();
}

$sql = "SELECT status FROM status WHERE id = ?";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $response['status'] = "success";
        $response['user_status'] = $row['status'] ? "online" : "offline";
    } else {
        $response['status'] = "failure";
        $response['message'] = "Status not found for the given user ID.";
    }

    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed.";
}

$conn->close();

echo json_encode($response);
?>
