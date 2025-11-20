<?php
include "./connect.php";
include "./headers.php";

$response = array();

// Check required POST fields
if (!isset($_POST['id']) || !isset($_POST['status'])) {
    $response['status'] = "failure";
    $response['message'] = "Required fields (id and status) are missing.";
    echo json_encode($response);
    exit();
}

$id = $_POST['id'];
$status_str = strtolower(trim($_POST['status']));

// Convert status string to boolean
if ($status_str === "online") {
    $status = true;
} elseif ($status_str === "offline") {
    $status = false;
} else {
    $response['status'] = "failure";
    $response['message'] = "Invalid status value. Must be 'online' or 'offline'.";
    echo json_encode($response);
    exit();
}

// Use INSERT ... ON DUPLICATE KEY UPDATE for upsert logic
$sql = "
    INSERT INTO status (id, status)
    VALUES (?, ?)
    ON DUPLICATE KEY UPDATE status = VALUES(status)
";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("ii", $id, $status);
    if ($stmt->execute()) {
        $response['status'] = "success";
        $response['message'] = "Status updated successfully.";
    } else {
        $response['status'] = "failure";
        $response['message'] = "Database execution failed.";
    }
    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed.";
}

$conn->close();

echo json_encode($response);
?>
