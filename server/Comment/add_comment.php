<?php
include "../connect.php";
include "../headers.php";

$response = array();

// Read POST input
$post_id = $_POST['post_id'] ?? null;
$user_id = $_POST['user_id'] ?? null;
$comment_text = $_POST['comment_text'] ?? null;

// Validate input
if (!$post_id || !$user_id || !$comment_text) {
    $response['status'] = "failure";
    $response['message'] = "post_id, user_id, and comment_text must be provided";
    echo json_encode($response);
    exit();
}

// Insert comment
// Note: Make sure to run migrate_comments_table.sql first to update the table structure
$sql = "INSERT INTO comments (post_id, user_id, comment_text) VALUES (?, ?, ?)";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("iis", $post_id, $user_id, $comment_text);
    if ($stmt->execute()) {
        $response['status'] = "success";
        $response['message'] = "Comment added successfully";
        $response['comment_id'] = $stmt->insert_id;
    } else {
        $response['status'] = "failure";
        $response['message'] = "Failed to add comment: " . $stmt->error;
    }
    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed";
}

$conn->close();

echo json_encode($response);
?>

