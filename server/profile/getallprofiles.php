<?php
include "../connect.php";
include "../headers.php";

$response = array();

$sql = "
    SELECT 
        id, 
        username, 
        picture 
    FROM 
        profile
";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $profiles = array();

        while ($row = $result->fetch_assoc()) {
            $profiles[] = $row;
        }

        $response['status'] = "success";
        $response['profiles'] = $profiles;

    } else {
        $response['status'] = "failure";
        $response['message'] = "No profiles found";
    }

    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed";
}

$conn->close();

echo json_encode($response);
?>
