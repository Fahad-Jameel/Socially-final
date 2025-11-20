<?php
include "../connect.php";
include "../headers.php";

$response = array();

// Read POST input
$userid = $_POST['userid'] ?? null;

// Validate input
if (!$userid) {
    $response['status'] = "failure";
    $response['message'] = "User ID not provided";
    echo json_encode($response);
    exit();
}

$sql = "
    SELECT 
        u.id,
        p.username,
        p.picture
    FROM 
        users u
    JOIN 
        profile p ON u.id = p.id
    WHERE 
        u.id != ?
        AND u.id NOT IN (
            SELECT fol_id FROM follow WHERE person_id = ?
        )
";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("ii", $userid, $userid);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $not_following = array();

        while ($row = $result->fetch_assoc()) {
            $not_following[] = $row;
        }

        $response['status'] = "success";
        $response['users'] = $not_following;
    } else {
        $response['status'] = "failure";
        $response['message'] = "No users found";
    }

    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed";
}

$conn->close();

echo json_encode($response);
?>
