<?php
include "../connect.php";
include "../headers.php";

$response = array();

// Read POST input
$post_id = $_POST['post_id'] ?? null;

// Validate input
if (!$post_id) {
    $response['status'] = "failure";
    $response['message'] = "post_id must be provided";
    echo json_encode($response);
    exit();
}

// Get comments for the post
// Note: Make sure to run migrate_comments_table.sql first to update the table structure
$sql = "
    SELECT 
        c.comment_id,
        c.comment_text,
        c.timestamp,
        u.username,
        p.picture as profile_pic
    FROM 
        comments c
    JOIN 
        users u ON c.user_id = u.id
    LEFT JOIN 
        profile p ON c.user_id = p.id
    WHERE 
        c.post_id = ?
    ORDER BY 
        c.timestamp ASC
";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("i", $post_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $comments = array();
    while ($row = $result->fetch_assoc()) {
        $comments[] = array(
            'comment_id' => $row['comment_id'],
            'comment_text' => $row['comment_text'],
            'username' => $row['username'],
            'profile_pic' => $row['profile_pic'] ?? '',
            'timestamp' => $row['timestamp']
        );
    }

    $response['status'] = "success";
    $response['comments'] = $comments;
    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed";
}

$conn->close();

echo json_encode($response);
?>

