<?php
include "../connect.php";
include "../headers.php";

$response = array();

$sql = "
    SELECT 
        profile.picture AS profile_picture,
        stories.picture AS story_picture
    FROM 
        profile
    JOIN 
        stories ON profile.id = stories.id
";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $stories = array();

        while ($row = $result->fetch_assoc()) {
            $stories[] = $row;
        }

        $response['status'] = "success";
        $response['stories'] = $stories;
    } else {
        $response['status'] = "failure";
        $response['message'] = "No stories found";
    }

    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed";
}

$conn->close();

echo json_encode($response);
?>
