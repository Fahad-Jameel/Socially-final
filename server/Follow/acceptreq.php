<?php
include "../connect.php";
include "../headers.php";

$response = array();

// Check if 'my_id' and 'uid' are provided in the POST request
if (!isset($_POST['my_id']) || !isset($_POST['uid'])) {
    $response['status'] = "failure";
    $response['message'] = "Both my_id and uid must be provided";
    echo json_encode($response);
    exit();
}

$my_id = $_POST['my_id'];
$uid = $_POST['uid'];

// SQL to insert the follow relationship
$sql = "INSERT INTO follow (person_id, fol_id) VALUES (?, ?)";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("ii", $my_id, $uid);
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
