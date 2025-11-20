<?php
include "../connect.php";
include "../headers.php";

$response = array();

// Read POST input
$person_id = $_POST['person_id'] ?? null;
$my_id = $_POST['my_id'] ?? null;

// Validate input
if (!$person_id || !$my_id) {
    $response['status'] = "failure";
    $response['message'] = "Both person_id and my_id must be provided";
    echo json_encode($response);
    exit();
}

// Insert follow relationship
$sql = "INSERT INTO follow (person_id, fol_id) VALUES (?, ?)";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("ii", $person_id, $my_id);
    if ($stmt->execute()) {
        $response['status'] = "success";
        $response['message'] = "Followed successfully";
    } else {
        $response['status'] = "failure";
        $response['message'] = "Failed to follow (maybe already followed or constraint error)";
    }
    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed";
}

$conn->close();

echo json_encode($response);
?>
