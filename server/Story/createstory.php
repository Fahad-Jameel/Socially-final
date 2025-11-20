<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_POST['id']) && isset($_POST['picture'])) {
    $id = (int)$_POST['id'];
    $picture = $_POST['picture'];

    $sql = "INSERT INTO stories (picture, id) VALUES (?, ?)";
    $stmt = $conn->prepare($sql);

    if ($stmt) {
        $stmt->bind_param("si", $picture, $id);

        if ($stmt->execute()) {
            $response['status'] = "success";
            $response['message'] = "Story created successfully";
            $response['story_id'] = $stmt->insert_id;
        } else {
            $response['status'] = "failure";
            $response['message'] = "Failed to insert story";
        }

        $stmt->close();
    } else {
        $response['status'] = "failure";
        $response['message'] = "Statement preparation failed";
    }

    $conn->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Missing required fields: id or picture";
}

echo json_encode($response);
?>
