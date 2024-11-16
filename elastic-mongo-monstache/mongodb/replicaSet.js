rs.initiate({
    _id : "replication",
    members: [
      {_id:0,host : "ip:27018"},
      {_id:1,host : "ip:27019"}
    ]
  } )

rs.conf();
