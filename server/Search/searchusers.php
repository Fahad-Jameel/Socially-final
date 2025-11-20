<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_POST['query'])) {
    $query = $_POST['query'] . '%'; // Add wildcard for "starts with"

    $sql = "SELECT username, picture FROM profile WHERE username LIKE ?";
    $stmt = $conn->prepare($sql);

    if ($stmt) {
        $stmt->bind_param("s", $query);
        $stmt->execute();
        $result = $stmt->get_result();

        $users = array();
        while ($row = $result->fetch_assoc()) {
            $users[] = $row;
        }

        $response['status'] = "success";
        $response['profiles'] = $users;

        $stmt->close();
    } else {
        $response['status'] = "failure";
        $response['message'] = "Statement preparation failed";
    }

    $conn->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Missing required field: query";
}

echo json_encode($response);
?>