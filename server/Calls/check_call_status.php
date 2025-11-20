<?php
include '../connect.php'; // your DB connection here
include '../headers.php'; // your header file here

$receiver_id = $_GET['receiver_id'];

$sql = "SELECT * FROM calls WHERE receiver_id = '$receiver_id' AND status = 'pending' ORDER BY timestamp DESC LIMIT 1";
$result = mysqli_query($conn, $sql);

if (mysqli_num_rows($result) > 0) {
    $row = mysqli_fetch_assoc($result);
    echo json_encode(["incoming" => true, "call_id" => $row['call_id'], "caller_id" => $row['caller_id']]);
} else {
    echo json_encode(["incoming" => false]);
}
?>
