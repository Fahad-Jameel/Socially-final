<?php
include "../connect.php";
include "../headers.php";

$response = array();

// Check if 'my_id' is provided in the POST request
if (!isset($_POST['my_id'])) {
    $response['status'] = "failure";
    $response['message'] = "User ID (my_id) not provided";
    echo json_encode($response);
    exit();
}

$my_id = (int)$_POST['my_id'];

// SQL to get the follower users' IDs
$sql = "
    SELECT 
        f.person_id AS follower_id, 
        u.username, 
        p.picture 
    FROM 
        follow f
    JOIN 
        users u ON f.person_id = u.id
    JOIN 
        profile p ON f.person_id = p.id
    WHERE 
        f.fol_id = ?
";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("i", $my_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $followers = array();

        while ($row = $result->fetch_assoc()) {
            $followers[] = array(
                'id' => $row['follower_id'],
                'username' => $row['username'],
                'profile_pic' => $row['picture']
            );
        }

        $response['status'] = "success";
        $response['followers'] = $followers;
    } else {
        $response['status'] = "failure";
        $response['message'] = "No followers found";
    }

    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed";
}

$conn->close();

echo json_encode($response);
?>
