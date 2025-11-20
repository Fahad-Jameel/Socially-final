<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_POST['user1'], $_POST['user2'])) {
    $user1 = $_POST['user1'];
    $user2 = $_POST['user2'];

    $sql = "SELECT * FROM messages 
            WHERE (sender_id = ? AND receiver_id = ?) 
               OR (sender_id = ? AND receiver_id = ?)
            ORDER BY timestamp ASC";

    $stmt = $conn->prepare($sql);

    if ($stmt) {
        $stmt->bind_param("iiii", $user1, $user2, $user2, $user1);
        $stmt->execute();
        $result = $stmt->get_result();

        $messages = array();
        while ($row = $result->fetch_assoc()) {
            $messages[] = $row;
        }

        $response['status'] = "success";
        $response['messages'] = $messages;

        $stmt->close();
    } else {
        $response['status'] = "failure";
        $response['message'] = "Statement preparation failed";
    }

    $conn->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Missing required fields";
}

echo json_encode($response);
?>
