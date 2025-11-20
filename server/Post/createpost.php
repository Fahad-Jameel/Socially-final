<?php
include "../connect.php";
include "../headers.php";

$response = array();

// Check if all required fields are set
if (isset($_POST['id']) && isset($_POST['picture']) && isset($_POST['caption'])) {
    // Get and sanitize data
    $id = (int)$_POST['id'];
    $picture = $_POST['picture']; // Base64 encoded image
    $caption = $_POST['caption'];
    $likes = 0; // Initialize likes to zero
    
    // Insert post into database
    $sql = "INSERT INTO posts (picture, likes, caption, id) VALUES (?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    if ($stmt) {
        $stmt->bind_param("sisi", $picture, $likes, $caption, $id);
        
        if ($stmt->execute()) {
            $post_id = $conn->insert_id;
            $response['status'] = "success";
            $response['message'] = "Post created successfully";
            $response['post_id'] = $post_id;
        } else {
            $response['status'] = "failure";
            $response['message'] = "Failed to create post: " . $stmt->error;
        }
        $stmt->close();
    } else {
        $response['status'] = "failure";
        $response['message'] = "Statement preparation failed: " . $conn->error;
    }
    $conn->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Missing required fields: id, picture, and caption are required";
}

echo json_encode($response);
?>