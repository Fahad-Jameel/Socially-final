<?php
include "../connect.php";
include "../headers.php";

$response = array();

$sql = "
    SELECT 
        posts.post_id,
        posts.picture,
        posts.likes,
        posts.caption,
        profile.picture AS profilepic,
        profile.username
    FROM 
        posts
    JOIN 
        profile ON posts.id = profile.id
";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $posts = array();

        while ($row = $result->fetch_assoc()) {
            $posts[] = $row;
        }

        $response['status'] = "success";
        $response['posts'] = $posts;
    } else {
        $response['status'] = "failure";
        $response['message'] = "No posts found";
    }

    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed";
}

$conn->close();

echo json_encode($response);
?>
