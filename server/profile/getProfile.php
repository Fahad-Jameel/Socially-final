<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_POST['id'])) {
    $id = (int)$_POST['id'];

    $sql = "SELECT * FROM profile WHERE id = ?";
    $stmt = $conn->prepare($sql);

    if ($stmt) {
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result && $result->num_rows > 0) {
            $profile = $result->fetch_assoc();
            $response['status']  = "success";
            $response['profile'] = $profile;
        } else {
            $response['status']  = "failure";
            $response['message'] = "Profile not found";
        }

        $stmt->close();
    } else {
        $response['status']  = "failure";
        $response['message'] = "Statement preparation failed";
    }

    $conn->close();
} else {
    $response['status']  = "failure";
    $response['message'] = "Missing required field: id";
}

echo json_encode($response);
?>
